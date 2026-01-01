# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require 'minitest/autorun'
require_relative 'test__helper'
require_relative '../objects/nb'
require_relative '../objects/humans'
require_relative '../objects/search'
require_relative '../objects/query'

# Test of Search.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::SearchTest < Minitest::Test
  def test_finds_messages
    human = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = human.bouts
    bout = bouts.start(test_name)
    msg = bout.post('Hey, you!')
    found = human.search(Nb::Query.new('(text=~you)'), 0, 10).to_a
    assert_equal(1, found.size)
    assert_equal(msg.id, found.first.id)
  end

  def test_finds_only_my_messages
    owner = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = owner.bouts
    bout = bouts.start(test_name)
    bout.post(test_name)
    friend = Nb::Humans.new(test_pgsql).take(test_name).create
    assert_equal(0, friend.search(Nb::Query.new(''), 0, 10).to_a.size)
  end

  def test_finds_by_tags_and_flags
    human = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = human.bouts
    bout = bouts.start(test_name)
    bout.post(test_name)
    bout.tags.put('weight', '150kg')
    bout.tags.put('color', 'blue')
    bout.post(test_name).flags.attach('xxl')
    bout.post(test_name).flags.attach('small')
    bout.post(test_name).flags.attach('medium')
    assert_equal(1, human.search(Nb::Query.new('(#color=blue and $small+)'), 0, 10).to_a.size)
  end

  def test_group_by_message
    human = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = human.bouts
    bout = bouts.start(test_name)
    msg = bout.post(test_name)
    msg.flags.attach('one')
    msg.flags.attach('two')
    assert_equal(1, human.search(Nb::Query.new(''), 0, 10).to_a.size)
  end

  def test_finds_by_date_intervals
    human = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = human.bouts
    bout = bouts.start(test_name)
    bout.post(test_name)
    assert_equal(1, human.search(Nb::Query.new("(posted<#{(DateTime.now + 1).iso8601(3)})"), 0, 10).to_a.size)
    assert_equal(0, human.search(Nb::Query.new("(posted>#{(DateTime.now + 1).iso8601(3)})"), 0, 10).to_a.size)
  end

  def test_finds_by_bout_owner
    human = Nb::Humans.new(test_pgsql).take(test_name).create
    friend = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = human.bouts
    bout = bouts.start(test_name)
    bout.post(test_name)
    bout.guests.invite(friend.identity)
    assert_equal(1, friend.search(Nb::Query.new("(owner=#{human.identity})"), 0, 10).to_a.size)
    assert_equal(0, friend.search(Nb::Query.new("(owner=#{friend.identity})"), 0, 10).to_a.size)
  end

  def test_finds_by_guests
    human = Nb::Humans.new(test_pgsql).take(test_name).create
    friend = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = human.bouts
    bout = bouts.start(test_name)
    bout.post(test_name)
    bout.guests.invite(friend.identity)
    assert_equal(1, friend.search(Nb::Query.new("(guest=#{friend.identity})"), 0, 10).to_a.size)
    assert_equal(0, friend.search(Nb::Query.new('(guest=somebody-else)'), 0, 10).to_a.size)
  end

  def test_search_by_absence
    human = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = human.bouts
    bout = bouts.start(test_name)
    bout.post(test_name).flags.attach('one')
    bout.post(test_name).flags.attach('one')
    bout.post(test_name)
    assert_equal(3, human.search(Nb::Query.new(''), 0, 10).to_a.size)
    assert_equal(1, human.search(Nb::Query.new('($one-)'), 0, 10).to_a.size)
  end

  def test_finds_only_my_bouts
    owner = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = owner.bouts
    bout = bouts.start(test_name)
    bout.post(test_name)
    friend = Nb::Humans.new(test_pgsql).take(test_name).create
    assert_empty(friend.search(Nb::Query.new("(bout=#{bout.id})"), 0, 10).to_a)
  end
end
