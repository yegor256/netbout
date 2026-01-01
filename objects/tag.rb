# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require_relative 'nb'
require_relative 'urror'

# Tag of a bout.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::Tag
  attr_reader :name

  def initialize(pgsql, bout, name)
    @pgsql = pgsql
    raise 'Bout is NULL' if bout.nil?
    @bout = bout
    raise 'Name is NULL' if name.nil?
    @name = name
  end

  def exists?
    !@pgsql.exec('SELECT * FROM tag WHERE bout=$1 AND name=$2', [@bout.id, @name]).empty?
  end

  def created
    @pgsql.exec('SELECT created FROM tag WHERE bout=$1 AND name=$2', [@bout.id, @name])[0]['created']
  end

  def value
    row = @pgsql.exec('SELECT value FROM tag WHERE bout=$1 AND name=$2', [@bout.id, @name])[0]
    raise Nb::Urror, "Tag '#{@name}' not found in the bout ##{@bout.id}" if row.nil?
    row['value']
  end

  def to_h
    {
      name: @name,
      value: value,
      created: created
    }
  end
end
