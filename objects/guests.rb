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

# Guests of a bout.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2024 Yegor Bugayenko
# License:: MIT
class Nb::Guests
  def initialize(pgsql, human, bout)
    @pgsql = pgsql
    raise 'Human is NULL' if human.nil?
    @human = human
    raise 'Bout is NULL' if bout.nil?
    @bout = bout
  end

  def invite(guest)
    raise Nb::Urror, "#{@human} can't invite guests to bout ##{@id}" unless @bout.mine?
    @pgsql.exec(
      'INSERT INTO guest (bout, human) VALUES ($1, $2)',
      [@bout.id, guest]
    )
  end

  def each
    raise Nb::Urror, "#{@human} can't list guests in bout ##{@id}" unless @bout.mine?
    @pgsql.exec('SELECT human FROM guest WHERE bout=$1', [@bout.id]).each do |row|
      yield row['human']
    end
  end

  def to_a
    raise Nb::Urror, "#{@human} can't serialize guests in bout ##{@id}" unless @bout.mine?
    array = []
    each { |g| array << g.to_h }
    array
  end
end
