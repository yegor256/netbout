# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

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
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
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
      assert_predicate(last_response, :ok?, last_response.body)
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
    post('/start', 'title=hello+world!')
    assert_equal(302, last_response.status, last_response.body)
    id = last_response.headers['X-Netbout-Bout'].to_i
    assert_predicate(id, :positive?)
    get("/bout/#{id}")
    assert_equal(200, last_response.status, last_response.body)
    json = JSON.parse(last_response.body)
    assert_equal(id, json['id'])
    get("/b/#{id}")
    assert_equal(302, last_response.status, last_response.body)
    assert_equal(last_response.headers['X-Netbout-Bout'].to_i, id)
  end

  def test_tag_bout
    login
    post('/start', 'title=hello+world!')
    assert_equal(302, last_response.status, last_response.body)
    id = last_response.headers['X-Netbout-Bout'].to_i
    assert_predicate(id, :positive?)
    get("/tags/#{id}")
    assert_equal(200, last_response.status, last_response.body)
    json = JSON.parse(last_response.body)
    assert_equal(0, json.size)
  end

  def test_post_message
    login
    post('/start', 'title=hello+world!')
    assert_equal(302, last_response.status, last_response.body)
    id = last_response.headers['X-Netbout-Bout'].to_i
    assert_predicate(id, :positive?)
    post("/b/#{id}/post", 'text=how+are+you')
    assert_equal(302, last_response.status, last_response.body)
    msg = last_response.headers['X-Netbout-Message'].to_i
    assert_predicate(msg, :positive?)
    get("/message/#{msg}")
    assert_equal(200, last_response.status, last_response.body)
    json = JSON.parse(last_response.body)
    assert_equal(msg, json['id'])
    get('/search')
    assert_equal(200, last_response.status, last_response.body)
    json = JSON.parse(last_response.body)
    assert_equal(msg, json[0]['id'])
  end

  def test_flag_message
    login
    post('/start', 'title=hello+world!')
    assert_equal(302, last_response.status, last_response.body)
    id = last_response.headers['X-Netbout-Bout'].to_i
    assert_predicate(id, :positive?)
    post("/b/#{id}/post", 'text=how+are+you')
    assert_equal(302, last_response.status, last_response.body)
    msg = last_response.headers['X-Netbout-Message'].to_i
    assert_predicate(msg, :positive?)
    name = 'some-flag'
    post("/m/#{msg}/attach", "name=#{name}")
    assert_equal(302, last_response.status, last_response.body)
    get("/message/#{msg}")
    assert_equal(200, last_response.status, last_response.body)
    json = JSON.parse(last_response.body)
    assert_equal(name, json['flags'][0]['name'])
    get("/flags/#{msg}")
    assert_equal(200, last_response.status, last_response.body)
    assert_equal(1, JSON.parse(last_response.body).size)
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
    refute_nil(last_response.headers['X-Netbout-Bout'])
  end

  private

  def login(name = test_name)
    enc = GLogin::Cookie::Open.new(
      { 'id' => name, 'login' => name },
      ''
    ).to_s
    set_cookie("identity=#{enc}")
  end
end
