# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require_relative 'nb'

# Human.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::Human
  attr_reader :identity

  def initialize(pgsql, identity)
    @pgsql = pgsql
    raise 'Identity is NULL' if identity.nil?
    raise 'Identity is empty' if identity.empty?
    @identity = identity
  end

  def admin?
    @identity == 'yegor256'
  end

  def to_s
    "@#{@identity}"
  end

  def create
    @pgsql.exec('INSERT INTO human (identity) VALUES ($1)', [@identity])
    self
  end

  def github=(login)
    @pgsql.exec('UPDATE human SET github = $1 WHERE identity = $2', [login, @identity])
  end

  def exists?
    !@pgsql.exec('SELECT * FROM human WHERE identity = $1', [@identity]).empty?
  end

  def telechat?
    !@pgsql.exec('SELECT telechat FROM human WHERE identity = $1', [@identity]).empty?
  end

  def tokens
    require_relative 'tokens'
    Nb::Tokens.new(@pgsql, self)
  end

  def bouts
    require_relative 'bouts'
    Nb::Bouts.new(@pgsql, self)
  end

  def messages
    require_relative 'messages'
    Nb::Messages.new(@pgsql, self)
  end

  def search(query, offset, limit)
    require_relative 'search'
    Nb::Search.new(@pgsql, self, query, offset, limit)
  end
end
