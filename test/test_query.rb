# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require 'minitest/autorun'
require_relative 'test__helper'
require_relative '../objects/nb'
require_relative '../objects/query'

# Test of Query.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::QueryTest < Minitest::Test
  def test_how_in_bout_works
    query = Nb::Query.new('(bout=3 and body=~hello and $foo+)')
    predicate = query.predicate
    bout = nil
    predicate.if_bout do |b|
      bout = b
    end
    assert_equal(3, bout)
  end

  def test_empty_query
    assert_equal('', Nb::Query.new('( ) ').predicate.to_s)
  end

  def test_easy_query
    assert_equal(
      '(bout=3 and #foo+ and (#bar+ and #bar=Hello))',
      Nb::Query.new('(bout=3 and #foo+ and #bar=Hello)').predicate.to_s
    )
  end

  def test_simple_query
    q = '(bout=3 and (body=~hello or $foo+))'
    query = Nb::Query.new(q)
    predicate = query.predicate
    assert_equal(q, predicate.to_s)
  end

  def test_moderate_query
    q = '((bout=3 or bout=6) and (body=~hello or $foo+))'
    query = Nb::Query.new(q)
    predicate = query.predicate
    assert_equal(q, predicate.to_s)
  end

  def test_complex_queries
    queries = [
      '(bout=3 and (body=~hello%20world! or (#foo- and #hello+) or ($green+ or $black-)) and posted>2024-09-24)',
      '(a=1 and (b=5 and c=8 and (c=9 and e=90) and (f=8 or i=90)))'
    ]
    queries.each do |q|
      query = Nb::Query.new(q)
      predicate = query.predicate
      assert_equal(q, predicate.to_s)
    end
  end

  def test_query_to_sql
    queries = {
      '(bout=3)' => 'bout.id = 3',
      '(title=~Hello&#x20;world!)' => 'bout.title LIKE \'%Hello world!%\'',
      '(title=A&apos;B)' => "bout.title = 'A\\'B'",
      '($foo+)' => "(flag.name='foo' AND flag.message IS NOT NULL)",
      '(#foo=bar)' => "((tag.name='foo' AND tag.bout IS NOT NULL) AND tag.value = 'bar')"
    }
    queries.each do |q, sql|
      query = Nb::Query.new(q)
      predicate = query.predicate
      assert_equal(sql, predicate.to_sql)
    end
  end
end
