# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require_relative 'nb'
require_relative 'urror'

# Tags of a bout.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::Tags
  def initialize(pgsql, human, bout)
    @pgsql = pgsql
    raise 'Human is NULL' if human.nil?
    @human = human
    raise 'Bout is NULL' if bout.nil?
    @bout = bout
  end

  def take(name)
    raise Nb::Urror, "#{@human} can't take a tag from bout ##{@id}" unless @bout.mine?
    require_relative 'tag'
    Nb::Tag.new(@pgsql, @bout, name)
  end

  def put(name, value)
    raise Nb::Urror, "#{@human} can't put a tag to bout ##{@id}" unless @bout.mine?
    raise Nb::Urror, "Invalid tag '#{name}'" unless name.match?(/^[a-z][a-z0-9-]+$/)
    @pgsql.exec(
      'INSERT INTO tag (bout, name, author, value) VALUES ($1, $2, $3, $4)',
      [@bout.id, name, @human.identity, value]
    )
    take(name)
  end

  def each
    raise Nb::Urror, "#{@human} can't list tags in bout ##{@id}" unless @bout.mine?
    @pgsql.exec('SELECT * FROM tag WHERE bout=$1', [@bout.id]).each do |row|
      yield take(row['name'])
    end
  end

  def to_a
    raise Nb::Urror, "#{@human} can't serialize tags in bout ##{@id}" unless @bout.mine?
    array = []
    each { |t| array << t.to_h }
    array
  end
end
