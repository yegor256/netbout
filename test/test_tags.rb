# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require 'minitest/autorun'
require_relative 'test__helper'
require_relative '../objects/nb'
require_relative '../objects/humans'
require_relative '../objects/bouts'

# Test of Tags.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::TagsTest < Minitest::Test
  def test_puts_tag_to_bout
    human = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = human.bouts
    bout = bouts.start('hi')
    key = 'a6364'
    bout.tags.put(key, 'Hello, друг!')
    tag = bout.tags.take(key)
    assert_predicate(tag, :exists?)
    assert(tag.value.start_with?('Hello'))
    bout.tags.each do |t|
      assert_predicate(t, :exists?)
    end
  end
end
