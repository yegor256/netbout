# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require_relative 'nb'
require_relative 'urror'

# Guests of a bout.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::Guests
  def initialize(pgsql, human, bout)
    @pgsql = pgsql
    raise 'Human is NULL' if human.nil?
    @human = human
    raise 'Bout is NULL' if bout.nil?
    @bout = bout
  end

  # Invite another user to this bout, where `guest` is the identity of the guest,
  # not an object.
  def invite(guest)
    raise Nb::Urror, "#{@human} can't invite guests to bout ##{@id}" unless @bout.mine?
    raise Nb::Urror, "@#{guest} is not a user" if @pgsql.exec(
      'SELECT identity FROM human WHERE identity=$1',
      [guest]
    ).empty?
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
    each { |g| array << g }
    array
  end
end
