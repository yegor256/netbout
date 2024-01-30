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

# Test of API front.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2024 Yegor Bugayenko
# License:: MIT
class Nb::ApiTest < Minitest::Test
  include Rack::Test::Methods

  def app
    Sinatra::Application
  end

  def test_search
    human = test_user
    bout = human.bouts.start('hey, друг!')
    bout.post('first one')
    bout.post('second one')
    get('/search')
    json = JSON.parse(last_response.body)
    assert_equal(2, json.size)
    assert(json.first['id'].positive?)
  end

  def test_bout
    human = test_user
    bout = human.bouts.start('hey, друг!')
    get("/bout/#{bout.id}")
    json = JSON.parse(last_response.body)
    assert_equal(bout.id, json['id'])
    assert(Time.parse(json['created']) < Time.now)
    assert_equal(human.identity, json['owner'])
    assert(json['title'].include?('друг'))
    assert(json['tags'].empty?)
    assert(json['guests'].empty?)
  end

  def test_message
    human = test_user
    bout = human.bouts.start('hello, друг!')
    msg = bout.post('how are you, товарищ?')
    get("/message/#{msg.id}")
    json = JSON.parse(last_response.body)
    assert_equal(msg.id, json['id'])
    assert_equal(bout.id, json['bout'])
    assert(Time.parse(json['created']) < Time.now)
    assert_equal(human.identity, json['author'])
    assert(json['text'].include?('товарищ'))
    assert(json['flags'].empty?)
  end

  def test_tags
    human = test_user
    bout = human.bouts.start('hey, друг!')
    bout.tags.put('foo1', 'как дела?')
    get("/tags/#{bout.id}")
    json = JSON.parse(last_response.body)
    assert_equal(1, json.size)
    assert_equal('foo1', json.first['name'])
    assert_equal('как дела?', json.first['value'])
  end

  def test_flags
    human = test_user
    bout = human.bouts.start('hello, друг!')
    msg = bout.post('just a test')
    msg.flags.attach('bar')
    get("/flags/#{msg.id}")
    json = JSON.parse(last_response.body)
    assert_equal(1, json.size)
    assert_equal('bar', json.first['name'])
    msg.flags.detach('bar')
    get("/flags/#{msg.id}")
    json = JSON.parse(last_response.body)
    assert(json.empty?)
  end

  private

  def test_user
    human = Nb::Humans.new(test_pgsql).take(test_name).create
    token = human.tokens.get
    header('X-Netbout-Token', token)
    human
  end
end
