# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require 'minitest/autorun'
require_relative 'test__helper'
require_relative '../objects/nb'
require_relative '../objects/humans'

# Test of Tokens.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::TokensTest < Minitest::Test
  def test_gets_token
    human = Nb::Humans.new(test_pgsql).take(test_name).create
    tokens = human.tokens
    token = tokens.get
    assert_equal(token, tokens.get)
  end
end
