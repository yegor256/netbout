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
      '(bout=3)' => ['bout.id = $1', ['3']],
      '(title=~Hello&#x20;world!)' => ['bout.title LIKE $1', ['%Hello world!%']],
      '(title=A&apos;B)' => ['bout.title = $1', ["A'B"]],
      '($foo+)' => ['(flag.name=$1 AND flag.message IS NOT NULL)', ['foo']],
      '(#foo=bar)' => ['((tag.name=$1 AND tag.bout IS NOT NULL) AND tag.value = $2)', %w[foo bar]],
      '(posted<2026-11-07)' => ['message.created<$1', ['2026-11-07']],
      '(started>2026-11-06)' => ['bout.created>$1', ['2026-11-06']],
      '($foo-)' => ['(SELECT COUNT(*) FROM flag AS f WHERE f.message=message.id AND name=$1) = 0', ['foo']],
      '(#foo-)' => ['(SELECT COUNT(*) FROM tag AS t WHERE t.bout=bout.id AND t.name=$1) = 0', ['foo']]
    }
    queries.each do |q, compiled|
      assert_equal(compiled, Nb::Query.new(q).compile)
    end
  end

  def test_keeps_special_values_out_of_sql
    ["O'Reilly", 'C:\\tmp', "M'OR'1'='1", 'A&apos;B', 'A&#39;B', '&#92;', '&amp;apos;'].each do |value|
      decoded = CGI.unescapeHTML(value)
      assert_equal(['message.text = $1', [decoded]], Nb::Query.new("(text=#{value})").compile)
      assert_equal(['message.text LIKE $1', ["%#{decoded}%"]], Nb::Query.new("(text=~#{value})").compile)
      assert_equal(['message.text<$1', [value]], Nb::Query.new("(text<#{value})").compile)
      assert_equal(['message.text>$1', [value]], Nb::Query.new("(text>#{value})").compile)
    end
  end

  def test_numbers_and_like_patterns_remain_bind_values
    assert_equal(['message.text = $1', ['00123']], Nb::Query.new('(text=00123)').compile)
    assert_equal(['bout.id = $1', ['0003']], Nb::Query.new('(bout=0003)').compile)
    assert_equal(['bout.id = $1::numeric', ['2147483648']], Nb::Query.new('(bout=2147483648)').compile)
    assert_equal(['message.text LIKE $1', ['%a_100\\%\\\\%']], Nb::Query.new('(text=~a_100\\%\\\\)').compile)
  end

  def test_compilation_reserves_parameters_and_is_repeatable
    values = ['alice'].freeze
    query = Nb::Query.new('(bout=3 and (text=~A&apos;B or (#kind=blue and $done-)))')
    sql = '(bout.id = $2 AND (message.text LIKE $3 OR (((tag.name=$4 AND tag.bout IS NOT NULL) ' \
          'AND tag.value = $5) AND (SELECT COUNT(*) FROM flag AS f WHERE f.message=message.id AND name=$6) = 0)))'
    expected = [sql, ['alice', '3', "%A'B%", 'kind', 'blue', 'done']]
    assert_equal(expected, query.compile(values))
    assert_equal(expected, query.compile(values))
    assert_equal(['alice'], values)
  end

  def test_compiles_blank_and_plain_text_queries
    assert_equal(['', []], Nb::Query.new('( ) ').compile)
    assert_equal(['message.text LIKE $1', ['%%']], Nb::Query.new('').compile)
    assert_equal(['message.text LIKE $1', ["%O'Reilly (notes)%"]], Nb::Query.new("O'Reilly (notes)").compile)
  end

  def test_rejects_unknown_attributes
    assert_raises(Nb::Urror) { Nb::Query.new('(unknown=value)').compile }
    assert_raises(Nb::Urror) { Nb::Query.new('($flag=value)').compile }
    assert_raises(Nb::Urror) { Nb::Query.new('(text+)').compile }
  end
end
