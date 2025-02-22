# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
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

# Test of API front.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2025 Yegor Bugayenko
# License:: MIT
class Nb::ApiTest < Minitest::Test
  include Rack::Test::Methods

  def app
    Sinatra::Application
  end

  def test_self
    human = test_user
    get('/self')
    json = JSON.parse(last_response.body)
    assert_equal(human.identity, json['identity'])
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

  def test_search_with_offset
    human = test_user
    bout = human.bouts.start('foom')
    bout.post('hey')
    get('/search?offset=100')
    json = JSON.parse(last_response.body)
    assert(json.empty?)
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
