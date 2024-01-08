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

require_relative 'nb'
require_relative 'urror'

# Search query string.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2024 Yegor Bugayenko
# License:: MIT
class Nb::Query
  def initialize(text)
    raise 'Text is NULL' if text.nil?
    @text = text
  end

  def predicate
    preds = @text.split(/\s+and\s+/i).map do |t|
      (left, right) = t.split('=')
      Eq.new(left.downcase, right)
    end
    And.new(preds)
  end

  class And
    def initialize(preds)
      @preds = preds
    end

    def if_bout
      @preds.each do |t|
        if t.is_a?(Eq)
          t.if_bout do |bout|
            yield bout
          end
        end
      end
    end

    def to_s
      @preds.map(&:to_s).join(' and ')
    end

    def to_sql
      if @preds.empty?
        '1=1'
      else
        @preds.map(&:to_sql).join(' AND ')
      end
    end
  end

  class Eq
    def initialize(left, right)
      @left = left
      @right = right
    end

    def if_bout
      yield @right.to_i if @left == 'bout'
    end

    def to_s
      "#{@left}=#{@right}"
    end

    def to_sql
      "#{@left} = #{@right}"
    end
  end
end
