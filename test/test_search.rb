# frozen_string_literal: true

# (The MIT License)
#
# Copyright (c) 2009-2024 Yegor Bugayenko
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the 'Software'), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

require 'minitest/autorun'
require_relative 'test__helper'
require_relative '../objects/nb'
require_relative '../objects/humans'
require_relative '../objects/search'
require_relative '../objects/query'

# Test of Search.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2024 Yegor Bugayenko
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
    assert_equal(0, human.search(Nb::Query.new("(posted>#{DateTime.now.iso8601(3)})"), 0, 10).to_a.size)
  end
end
