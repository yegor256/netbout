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

  def test_searches_apostrophes
    human, bout = search_fixture
    msg = bout.post("O'Reilly")
    bout.post('Unrelated')
    ["(text=O'Reilly)", "(text=~O'Reilly)", "O'Reilly", '(text=O&apos;Reilly)', '(text=~O&#39;Reilly)'].each do |q|
      assert_equal([msg.id], search_ids(human, q), q)
    end
  end

  def test_searches_backslashes_with_equality
    human, bout = search_fixture
    msg = bout.post('C:\\tmp\\file')
    bout.post('C:tmpfile')
    ['(text=C:\\tmp\\file)', '(text=C:&#92;tmp&#92;file)'].each do |q|
      assert_equal([msg.id], search_ids(human, q), q)
    end
  end

  def test_preserves_like_patterns
    human, bout = search_fixture
    literal = bout.post('a_b 100% C:\\tmp')
    wildcard = bout.post('axb 100x C:tmp')
    {
      'a_b' => [literal.id, wildcard.id],
      'a\\_b' => [literal.id],
      '100%' => [literal.id, wildcard.id],
      '100\\%' => [literal.id],
      'C:\\\\tmp' => [literal.id],
      'C:\\tmp' => [wildcard.id]
    }.each do |pattern, ids|
      assert_equal(ids.sort, search_ids(human, "(text=~#{pattern})"), pattern)
    end
  end

  def test_decodes_html_once_for_equality_and_contains
    human, bout = search_fixture
    msg = bout.post("O'Reilly & <notes>")
    encoded = 'O&apos;Reilly&#x20;&amp;&#x20;&lt;notes&gt;'
    assert_equal([msg.id], search_ids(human, "(text=#{encoded})"))
    assert_equal([msg.id], search_ids(human, "(text=~#{encoded})"))
    literal = bout.post('&apos;')
    assert_equal([literal.id], search_ids(human, '(text=&amp;apos;)'))
  end

  def test_compares_apostrophes_and_backslashes
    human, bout = search_fixture
    lower = bout.post('A')
    upper = bout.post('Z')
    ["O'Reilly", 'M\\iddle'].each do |value|
      assert_equal([lower.id], search_ids(human, "(text<#{value})"))
      assert_equal([upper.id], search_ids(human, "(text>#{value})"))
    end
  end

  def test_preserves_raw_html_operands_for_comparisons
    human, bout = search_fixture
    bout.post('M&apos;')
    assert_empty(search_ids(human, '(text<M&apos;)'))
    assert_empty(search_ids(human, '(text>M&apos;)'))
  end

  def test_sql_shaped_values_cannot_change_predicates_or_access
    human, bout = search_fixture
    lower = bout.post('A')
    upper = bout.post('Z')
    _, foreign = search_fixture
    foreign.post('A')
    foreign.post('Z')
    value = "M'OR'1'='1"
    assert_empty(search_ids(human, "(text=#{value})"))
    assert_empty(search_ids(human, "(text=~#{value})"))
    assert_equal([lower.id], search_ids(human, "(text<#{value})"))
    assert_equal([upper.id], search_ids(human, "(text>#{value})"))
  end

  def test_sql_shaped_values_can_be_found_as_data
    human, bout = search_fixture
    value = "M'OR'1'='1"
    msg = bout.post(value)
    bout.post('Unrelated')
    ['=', '=~'].each do |operator|
      assert_equal([msg.id], search_ids(human, "(text#{operator}#{value})"))
    end
  end

  def test_owner_and_guest_access_with_nested_or
    human, own = search_fixture
    first = own.post('alpha')
    _, shared = search_fixture
    second = shared.post('beta')
    shared.guests.invite(human.identity)
    _, foreign = search_fixture
    foreign.post('alpha')
    foreign.post('beta')
    query = "((text=alpha or text=beta) and (bout=#{own.id} or bout=#{shared.id} or bout=#{foreign.id}))"
    assert_equal([first.id, second.id].sort, search_ids(human, query))
    assert_empty(search_ids(human, "(bout=#{foreign.id})"))
  end

  def test_preserves_numeric_bout_equality_and_tag_absence
    human, bout = search_fixture
    msg = bout.post('123')
    assert_equal([msg.id], search_ids(human, "(bout=00#{bout.id})"))
    assert_empty(search_ids(human, '(bout=2147483648)'))
    assert_empty(search_ids(human, '(bout=9223372036854775808)'))
    assert_equal([msg.id], search_ids(human, '(#color-)'))
    bout.tags.put('color', 'blue')
    assert_empty(search_ids(human, '(#color-)'))
    assert_equal([msg.id], search_ids(human, '(#color=blue)'))
  end

  private

  def search_fixture
    human = Nb::Humans.new(test_pgsql).take(test_name).create
    [human, human.bouts.start(test_name)]
  end

  def search_ids(human, query)
    human.search(Nb::Query.new(query), 0, 100).to_a.map(&:id).sort
  end
end
