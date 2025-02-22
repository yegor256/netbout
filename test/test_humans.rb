# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require 'minitest/autorun'
require_relative 'test__helper'
require_relative '../objects/nb'
require_relative '../objects/humans'

# Test of Humans.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2025 Yegor Bugayenko
# License:: MIT
class Nb::HumansTest < Minitest::Test
  def test_adds_and_reads
    identity = test_name
    humans = Nb::Humans.new(test_pgsql)
    human = humans.take(identity)
    assert(human.identity == identity)
    assert(!human.exists?)
    human.create
    assert(human.exists?)
  end

  def test_adds_and_finds_by_github
    identity = test_name
    login = test_name
    humans = Nb::Humans.new(test_pgsql)
    assert(!humans.github?(login))
    human = humans.take(identity)
    human.create
    human.github = login
    assert(humans.github?(login))
    assert(humans.take_by_github(login).exists?)
  end

  def test_starts_bout
    human = Nb::Humans.new(test_pgsql).take(test_name)
    human.create
    bout = human.bouts.start('hello!')
    assert(bout.id.positive?)
  end

  def test_finds_by_token
    humans = Nb::Humans.new(test_pgsql)
    human = humans.take(test_name).create
    tokens = human.tokens
    token = tokens.get
    assert_equal(human.identity, humans.find_by_token(token).identity)
  end
end
