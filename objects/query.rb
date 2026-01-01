# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require 'cgi'
require_relative 'nb'
require_relative 'urror'

# Search query string.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::Query
  def initialize(text)
    raise 'Text is NULL' if text.nil?
    @text = text
  end

  def predicate
    q = @text
    unless @text.start_with?('(')
      q = CGI.escapeHTML(q)
        .gsub(' ', '&#x20;')
        .gsub('(', '&#x28;')
        .gsub(')', '&#x29;')
      q = "(text=~#{q})"
    end
    (pred,) = to_ast(to_terms(q), 0)
    pred
  end

  # AND
  class And
    def initialize(preds)
      @preds = preds
    end

    def if_bout(&block)
      @preds.each do |t|
        t.if_bout(&block)
      end
    end

    def to_s
      "(#{@preds.map(&:to_s).join(' and ')})"
    end

    def to_sql
      if @preds.empty?
        '1=1'
      else
        "(#{@preds.map(&:to_sql).join(' AND ')})"
      end
    end
  end

  # OR
  class Or
    def initialize(preds)
      @preds = preds
    end

    def if_bout(&block)
      @preds.each do |t|
        t.if_bout(&block)
      end
    end

    def to_s
      "(#{@preds.map(&:to_s).join(' or ')})"
    end

    def to_sql
      if @preds.empty?
        '1=1'
      else
        "(#{@preds.map(&:to_sql).join(' OR ')})"
      end
    end
  end

  # EQ
  class Eq
    def initialize(left, right)
      @left = left
      @right = right
    end

    def if_bout
      yield @right.to_s.to_i if @left.to_s == 'bout'
    end

    def to_s
      "#{@left}=#{@right}"
    end

    def to_sql
      r = @right.to_sql
      r = "'#{r}'" unless r.match?(/^[0-9]+$/)
      "#{@left.to_sql} = #{r}"
    end
  end

  # Less than
  class Lt
    def initialize(left, right)
      @left = left
      @right = right
    end

    def if_bout
      # yield nothing
    end

    def to_s
      "#{@left}<#{@right}"
    end

    def to_sql
      "#{@left.to_sql}<'#{@right}'"
    end
  end

  # Greater than
  class Gt
    def initialize(left, right)
      @left = left
      @right = right
    end

    def if_bout
      # yield nothing
    end

    def to_s
      "#{@left}>#{@right}"
    end

    def to_sql
      "#{@left.to_sql}>'#{@right}'"
    end
  end

  # ABSENT or PRESENT
  class Prabsent
    def initialize(present, tag)
      @present = present
      @tag = tag
    end

    def if_bout
      # yield nothing
    end

    def to_s
      "#{@tag}#{@present ? '+' : '-'}"
    end

    def to_sql
      @tag.to_prabsent(@present)
    end
  end

  # CONTAINS
  class Contains
    def initialize(left, right)
      @left = left
      @right = right
    end

    def if_bout
      # yield nothing
    end

    def to_s
      "#{@left}=~#{@right}"
    end

    def to_sql
      "#{@left.to_sql} LIKE '%#{@right.to_sql}%'"
    end
  end

  # Blank
  class Blank
    def if_bout
      # yield nothing
    end

    def to_s
      ''
    end

    def to_sql
      ''
    end
  end

  # Term in AST
  class Term
    attr_reader :kind, :left, :right, :op, :prefix, :src

    def initialize(kind, left: nil, right: nil, op: nil, src: nil)
      @kind = kind
      @left = left
      @right = right
      @op = op
      @src = src
    end

    def to_s
      "#{@kind}<#{@left}#{@op}#{@right}>"
    end

    def to_pred
      case @op
      when '='
        eq = Eq.new(@left, @right)
        if @left.attr?
          eq
        else
          And.new([Prabsent.new(true, @left), eq])
        end
      when '=~'
        Contains.new(@left, @right)
      when '-'
        Prabsent.new(false, @left)
      when '+'
        Prabsent.new(true, @left)
      when '<'
        Lt.new(@left, @right)
      when '>'
        Gt.new(@left, @right)
      else
        raise Nb::Urror, "Unknown operator '#{@op}'"
      end
    end
  end

  # Left operand
  class Left
    def initialize(prefix, name)
      @prefix = prefix
      @name = name
    end

    def attr?
      @prefix.nil?
    end

    def to_s
      "#{@prefix}#{@name}"
    end

    def to_sql
      if attr?
        case @name
        when 'bout'
          'bout.id'
        when 'text'
          'message.text'
        when 'title'
          'bout.title'
        when 'owner'
          'bout.owner'
        when 'started'
          'bout.created'
        when 'posted'
          'message.created'
        when 'guest'
          'guest.human'
        else
          raise Nb::Urror, "Unknown attribute '#{@name}'"
        end
      elsif @prefix == '#'
        'tag.value'
      elsif @prefix == '$'
        raise Nb::Urror, "Can't use flag '#{@name}' as a lefty"
      else
        raise Nb::Urror, "Unknown prefix '#{@prefix}'"
      end
    end

    def to_prabsent(present)
      if present
        head = if @prefix == '#'
          "tag.name='#{@name}'"
        elsif @prefix == '$'
          "flag.name='#{@name}'"
        else
          raise Nb::Urror, "Can't use prabsent on '#{@name}' attribute"
        end
        tail = if @prefix == '#'
          'tag.bout IS NOT NULL'
        elsif @prefix == '$'
          'flag.message IS NOT NULL'
        else
          raise Nb::Urror, "Can't use prabsent on '#{@name}' attribute"
        end
        return "(#{head} AND #{tail})"
      end
      if @prefix == '#'
        "(SELECT COUNT(*) FROM tag AS t WHERE t.bout=bout.id AND t.name='#{@name}') = 0"
      elsif @prefix == '$'
        "(SELECT COUNT(*) FROM flag AS f WHERE f.message=message.id AND name='#{@name}') = 0"
      else
        raise Nb::Urror, "Can't use prabsent on '#{@name}' attribute"
      end
    end
  end

  # Right operand
  class Right
    def initialize(value)
      @value = value
    end

    def to_s
      @value
    end

    def to_sql
      CGI.unescapeHTML(@value).gsub("'", "\\\\'")
    end
  end

  private

  def to_terms(txt)
    list = []
    acc = ''
    esc = [' ', ')']
    "#{txt} ".chars.each do |c|
      if !acc.empty? && esc.include?(c)
        list << if acc == 'and'
          Term.new(:AND)
        elsif acc == 'or'
          Term.new(:OR)
        else
          m = acc.match(/^(?<prefix>#|\$)?(?<left>[a-z]+)(?<op>=~|\+|-|=|<|>)(?<right>.*)?$/)
          raise Nb::Urror, "Can't parse '#{@text}' at '#{acc}'" if m.nil?
          Term.new(:TERM, left: Left.new(m[:prefix], m[:left]), right: Right.new(m[:right]), op: m[:op], src: acc)
        end
        acc = ''
      end
      case c
      when '('
        list << Term.new(:OPEN)
      when ')'
        list << Term.new(:CLOSE)
      when ' '
        # ignore it
      else
        acc += c
      end
    end
    list
  end

  # Takes a list of terms and a position where to start parsing.
  # Returns a pred and a new position for parsing continuing.
  def to_ast(terms, at)
    pred = if terms[at].kind == :OPEN
      op = nil
      operands = []
      loop do
        at += 1
        break if terms[at].kind == :CLOSE
        (operand, at1) = to_ast(terms, at)
        at = at1
        operands << operand
        break if terms[at].kind == :CLOSE
        raise Nb::Urror, "Use brackets at #{at}: #{op} -> #{terms[at]}" if !op.nil? && op.kind != terms[at].kind
        op = terms[at]
      end
      if operands.empty?
        Blank.new
      elsif op.nil?
        operands[0]
      elsif op.kind == :AND
        And.new(operands)
      elsif op.kind == :OR
        Or.new(operands)
      else
        raise Nb::Urror, "Unknown operator #{op}"
      end
    else
      terms[at].to_pred
    end
    [pred, at + 1]
  end
end
