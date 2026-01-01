# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require_relative 'nb'
require_relative 'urror'

# Flag of a message.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::Flag
  attr_reader :name

  def initialize(pgsql, message, name)
    @pgsql = pgsql
    raise 'Bout is NULL' if message.nil?
    @message = message
    raise 'Name is NULL' if name.nil?
    @name = name
  end

  def exists?
    !@pgsql.exec('SELECT * FROM flag WHERE message=$1 AND name=$2', [@message.id, @name]).empty?
  end

  def created
    @pgsql.exec('SELECT created FROM flag WHERE message=$1 AND name=$2', [@message.id, @name])[0]['created']
  end

  def detach
    @pgsql.exec(
      'DELETE FROM flag WHERE message=$1 AND name=$2',
      [@message.id, @name]
    )
  end

  def to_h
    {
      name: @name,
      created: created
    }
  end
end
