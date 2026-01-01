# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require 'minitest/autorun'
require_relative 'test__helper'
require_relative '../objects/nb'
require_relative '../objects/humans'
require_relative '../objects/bouts'

# Test of Guests.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::GuestsTest < Minitest::Test
  def test_invites_to_bout
    human = Nb::Humans.new(test_pgsql).take(test_name).create
    friend = Nb::Humans.new(test_pgsql).take(test_name).create
    id = human.bouts.start('hi').id
    human.bouts.take(id).guests.invite(friend.identity)
    assert_predicate(human.bouts.take(id), :mine?)
    bout = friend.bouts.take(id)
    assert_predicate(bout, :mine?)
    found = bout.guests.to_a.map do |t|
      t
    end
    assert_equal(1, found.size)
    assert_equal(friend.identity, found.first)
  end
end
