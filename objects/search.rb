# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require_relative 'nb'

# Search results.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
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
