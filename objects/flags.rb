# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require_relative 'nb'
require_relative 'urror'

# Flags of a bout.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::Flags
  def initialize(pgsql, human, message)
    @pgsql = pgsql
    raise 'Human is NULL' if human.nil?
    @human = human
    raise 'Message is NULL' if message.nil?
    @message = message
  end

  def take(name)
    raise Nb::Urror, "#{@human} can't take a flag from message ##{@id}" unless @message.mine?
    require_relative 'flag'
    Nb::Flag.new(@pgsql, @message, name)
  end

  def attach(name)
    raise Nb::Urror, "#{@human} can't attach a flag to message ##{@id}" unless @message.mine?
    raise Nb::Urror, "Invalid flag '#{name}'" unless name.match?(/^[a-z][a-z0-9-]+$/)
    @pgsql.exec(
      'INSERT INTO flag (message, name, author) VALUES ($1, $2, $3)',
      [@message.id, name, @human.identity]
    )
    take(name)
  end

  def detach(name)
    raise Nb::Urror, "#{@human} can't detach a flag from message ##{@id}" unless @message.mine?
    @pgsql.exec(
      'DELETE FROM flag WHERE message=$1 AND name=$2',
      [@message.id, name]
    )
  end

  def each
    raise Nb::Urror, "#{@human} can't list flags in message ##{@id}" unless @message.mine?
    @pgsql.exec('SELECT * FROM flag WHERE message=$1', [@message.id]).each do |row|
      yield take(row['name'])
    end
  end

  def to_a
    raise Nb::Urror, "#{@human} can't serialize flags in message ##{@id}" unless @message.mine?
    array = []
    each { |f| array << f.to_h }
    array
  end
end
