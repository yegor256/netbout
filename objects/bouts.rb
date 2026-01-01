# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require_relative 'nb'
require_relative 'urror'

# Bouts of a human.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::Bouts
  # When bout is not found by ID
  class BoutNotFound < Nb::Urror; end

  # When title is not current
  class WrongTitle < Nb::Urror; end

  def initialize(pgsql, human)
    @pgsql = pgsql
    raise 'Human is NULL' if human.nil?
    @human = human
  end

  def start(title)
    raise 'Title is NULL' if title.nil?
    raise WrongTitle, "The title can't be empty" if title.empty?
    rows = @pgsql.exec('INSERT INTO bout (owner, title) VALUES ($1, $2) RETURNING id', [@human.identity, title])
    id = rows[0]['id'].to_i
    take(id)
  end

  def take(id)
    raise "Bout ID '#{id}' must be integer" unless id.is_a?(Integer)
    raise BoutNotFound, "The bout ##{id} doesn't exist" unless exists?(id)
    require_relative 'bout'
    Nb::Bout.new(@pgsql, @human, id)
  end

  def exists?(id)
    raise "Bout ID '#{id}' must be integer" unless id.is_a?(Integer)
    !@pgsql.exec('SELECT id FROM bout WHERE id = $1', [id]).empty?
  end
end
