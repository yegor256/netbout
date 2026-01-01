# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require 'minitest/autorun'
require_relative 'test__helper'
require_relative '../objects/nb'
require_relative '../objects/humans'

# Test of Message.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::MessageTest < Minitest::Test
  def test_turns_into_json
    owner = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = owner.bouts
    bout = bouts.start('hi, друг!')
    msg = bout.post('how are you, друг?')
    json = msg.to_h
    assert_predicate(json[:id], :positive?)
    assert_includes(json[:text], 'друг')
    assert_operator(json[:created], :<, Time.now)
    assert_empty(json[:flags])
  end
end
