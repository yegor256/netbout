# frozen_string_literal: true

# (The MIT License)
#
# Copyright (c) 2009-2024 Yegor Bugayenko
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the 'Software'), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

require 'minitest/autorun'
require 'rack/test'
require 'json'
require_relative 'test__helper'
require_relative '../netbout'
require_relative '../objects/nb'
require_relative '../objects/humans'

module Rack
  module Test
    class Session
      def default_env
        { 'REMOTE_ADDR' => '127.0.0.1', 'HTTPS' => 'on' }.merge(headers_for_env)
      end
    end
  end
end

# Test of web front.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2024 Yegor Bugayenko
# License:: MIT
class Nb::AppTest < Minitest::Test
  include Rack::Test::Methods

  def app
    Sinatra::Application
  end

  def test_renders_pages
    pages = [
      '/version',
      '/robots.txt',
      '/',
      '/logo.svg'
    ]
    pages.each do |p|
      get(p)
      assert(last_response.ok?, last_response.body)
    end
  end

  def test_not_found
    ['/unknown_path', '/js/x/y/z/not-found.js', '/css/a/b/c/not-found.css'].each do |p|
      get(p)
      assert_equal(404, last_response.status, last_response.body)
      assert_equal('text/html;charset=utf-8', last_response.content_type)
    end
  end

  def test_200_user_pages
    login
    pages = [
      '/inbox'
    ]
    pages.each do |p|
      get(p)
      assert_equal(200, last_response.status, "#{p} fails: #{last_response.body}")
    end
  end

  def test_create_bout
    login
    post(
      '/start',
      'title=hello+world!'
    )
    assert_equal(302, last_response.status, last_response.body)
    id = last_response.headers['X-Netbout-Bout'].to_i
    get("/bout/#{id}")
    assert_equal(200, last_response.status, last_response.body)
    json = JSON.parse(last_response.body)
    assert_equal(id, json['id'])
    get("/b/#{id}")
    assert_equal(302, last_response.status, last_response.body)
    assert_equal(last_response.headers['X-Netbout-Bout'].to_i, id)
  end

  def test_post_message
    login
    post('/start', 'title=hello+world!')
    assert_equal(302, last_response.status, last_response.body)
    id = last_response.headers['X-Netbout-Bout']
    post("/b/#{id}/post", 'text=how+are+you')
    assert_equal(302, last_response.status, last_response.body)
    msg = last_response.headers['X-Netbout-Message'].to_i
    get("/message/#{msg}")
    assert_equal(200, last_response.status, last_response.body)
    json = JSON.parse(last_response.body)
    assert_equal(msg, json['id'])
  end

  def test_flag_message
    login
    post('/start', 'title=hello+world!')
    assert_equal(302, last_response.status, last_response.body)
    id = last_response.headers['X-Netbout-Bout']
    post("/b/#{id}/post", 'text=how+are+you')
    assert_equal(302, last_response.status, last_response.body)
    msg = last_response.headers['X-Netbout-Message']
    name = 'some-flag'
    post("/m/#{msg}/attach", "name=#{name}")
    assert_equal(302, last_response.status, last_response.body)
    get("/message/#{msg}")
    assert_equal(200, last_response.status, last_response.body)
    json = JSON.parse(last_response.body)
    assert_equal(name, json['flags'][0]['name'])
  end

  def test_github_callback
    get('/github-callback?code=99999')
    assert_equal(302, last_response.status, last_response.body)
  end

  def test_login_via_token
    human = Nb::Humans.new(test_pgsql).take(test_name).create
    token = human.tokens.get
    header('X-Netbout-Token', token)
    post('/start', 'title=hello+world!')
    assert_equal(302, last_response.status, last_response.body)
    assert(!last_response.headers['X-Netbout-Bout'].nil?)
  end

  private

  def login(name = test_name)
    set_cookie("identity=#{name}|#{name}")
  end
end
