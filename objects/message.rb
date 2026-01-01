# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require_relative 'nb'
require_relative 'bout'
require_relative 'humans'
require_relative 'flags'

# Message of a user (reader).
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::Message
  attr_reader :id

  def initialize(pgsql, human, id)
    @pgsql = pgsql
    raise 'Human is NULL' if human.nil?
    @human = human
    raise 'Id is NULL' if id.nil?
    @id = id
  end

  def mine?
    !@pgsql.exec(
      [
        'SELECT message.id FROM message',
        'LEFT JOIN bout ON message.bout=bout.id',
        'LEFT JOIN guest ON message.bout=guest.bout',
        'WHERE bout.owner=$1 OR guest.human=$1 AND message.id=$2',
        'LIMIT 1'
      ],
      [@human.identity, @id]
    ).empty?
  end

  def exists?
    !@pgsql.exec('SELECT author FROM message WHERE id = $1', [@id]).empty?
  end

  def text
    raise Nb::Urror, "#{@human} can't read message ##{@id}" unless mine?
    @pgsql.exec('SELECT text FROM message WHERE id = $1', [@id])[0]['text']
  end

  def created
    raise Nb::Urror, "#{@human} can't read message ##{@id}" unless mine?
    time = @pgsql.exec('SELECT created FROM message WHERE id = $1', [@id])[0]['created']
    Time.parse(time)
  end

  def author
    raise Nb::Urror, "#{@human} can't read message ##{@id}" unless mine?
    author = @pgsql.exec('SELECT author FROM message WHERE id = $1', [@id])[0]['author']
    Nb::Humans.new(@pgsql).take(author)
  end

  def bout
    raise Nb::Urror, "#{@human} can't read message ##{@id}" unless mine?
    bout = @pgsql.exec('SELECT bout FROM message WHERE id = $1', [@id])[0]['bout'].to_i
    Nb::Bout.new(@pgsql, @human, bout)
  end

  def flags
    raise Nb::Urror, "#{@human} can't read message ##{@id}" unless mine?
    Nb::Flags.new(@pgsql, @human, self)
  end

  def to_h
    raise Nb::Urror, "#{@human} can't serialize message ##{@id}" unless mine?
    {
      id: @id,
      bout: bout.id,
      text: text,
      created: created,
      author: author.identity,
      flags: flags.to_a
    }
  end
end
