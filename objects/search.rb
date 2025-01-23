# frozen_string_literal: true

# (The MIT License)
#
# Copyright (c) 2009-2025 Yegor Bugayenko
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

# Search results.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2025 Yegor Bugayenko
# License:: MIT
class Nb::Search
  def initialize(pgsql, identity, query, offset, limit)
    @pgsql = pgsql
    raise 'Identity is NULL' if identity.nil?
    @identity = identity
    raise 'Query is NULL' if query.nil?
    @query = query
    @offset = offset
    raise Urror::Nb, 'Limit can\'t be larger than 1024' if limit > 1024
    @limit = limit
  end

  def to_a
    array = []
    each do |m|
      array << m
    end
    array
  end

  def each
    require_relative 'message'
    pred = @query.predicate.to_sql
    pred = "AND #{pred}" unless pred.empty?
    q = [
      'SELECT message.id FROM message',
      'JOIN bout ON message.bout = bout.id',
      'LEFT JOIN guest ON guest.bout = bout.id',
      'LEFT JOIN tag ON tag.bout = bout.id',
      'LEFT JOIN flag ON flag.message = message.id',
      'WHERE (bout.owner = $1 OR guest.human = $1)',
      pred,
      'GROUP BY message.id',
      'ORDER BY message.created DESC',
      "OFFSET #{@offset}",
      "LIMIT #{@limit}"
    ].join(' ')
    @pgsql.exec(q, [@identity.identity]).each do |row|
      id = row['id'].to_i
      yield Nb::Message.new(@pgsql, @identity, id)
    end
  end
end
