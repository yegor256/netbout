# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require 'minitest/autorun'
require_relative 'test__helper'
require_relative '../objects/nb'
require_relative '../objects/humans'
require_relative '../objects/bouts'
require_relative '../objects/urror'

# Test of Bout.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::BoutTest < Minitest::Test
  def test_access_denied_on_post
    owner = Nb::Humans.new(test_pgsql).take(test_name).create
    guest = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = owner.bouts
    id = bouts.start('hi').id
    owner.bouts.take(id).post('Hello!')
    bout = guest.bouts.take(id)
    refute_predicate(bout, :mine?)
    assert_raises Nb::Urror do
      bout.post('I can see you :(')
    end
  end

  def test_checks_existence
    owner = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = owner.bouts
    id = bouts.start('hi').id
    assert_predicate(owner.bouts.take(id), :exists?)
  end

  def test_prohibits_existence_checking
    owner = Nb::Humans.new(test_pgsql).take(test_name).create
    guest = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = owner.bouts
    id = bouts.start('hi').id
    bout = guest.bouts.take(id)
    refute_predicate(bout, :mine?)
    assert_predicate(bout, :exists?)
  end

  def test_turns_into_json
    owner = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = owner.bouts
    bout = bouts.start('hi, друг!')
    json = bout.to_h
    assert_predicate(json[:id], :positive?)
    assert_includes(json[:title], 'друг')
    assert_operator(json[:created], :<, Time.now)
    assert_empty(json[:tags])
    assert_empty(json[:guests])
  end

  def test_checks_permission
    owner = Nb::Humans.new(test_pgsql).take(test_name).create
    friend = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = owner.bouts
    bout = bouts.start('hi')
    bout.post(test_name)
    refute_predicate(friend.bouts.take(bout.id), :mine?)
    bout.guests.invite(friend.identity)
    assert_predicate(friend.bouts.take(bout.id), :mine?)
  end
end
