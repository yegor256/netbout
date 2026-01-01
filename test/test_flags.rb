# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require 'minitest/autorun'
require_relative 'test__helper'
require_relative '../objects/nb'
require_relative '../objects/humans'
require_relative '../objects/bouts'

# Test of Flags.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::FlagsTest < Minitest::Test
  def test_attaches_flags_to_message
    human = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = human.bouts
    bout = bouts.start('hi')
    msg = bout.post('How are you?')
    flag = msg.flags.attach('hey')
    assert_predicate(flag, :exists?)
    msg.flags.each do |f|
      assert_predicate(f, :exists?)
    end
  end

  def test_attaches_and_detaches
    human = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = human.bouts
    bout = bouts.start('ooo')
    msg = bout.post('hey you')
    key = 'the-flag'
    flag = msg.flags.attach(key)
    assert_predicate(flag, :exists?)
    flag.detach
    refute_predicate(flag, :exists?)
  end
end
