# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require_relative 'nb'
require_relative 'urror'

# Humans.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::Humans
  def initialize(pgsql)
    @pgsql = pgsql
  end

  def take(identity)
    require_relative 'human'
    Nb::Human.new(@pgsql, identity)
  end

  def find_by_token(sha)
    rows = @pgsql.exec('SELECT human FROM token WHERE sha=$1', [sha])
    raise Nb::Urror, "Can't find a human by #{'*' * sha.length} token" if rows.empty?
    take(rows[0]['human'])
  end

  def take_by_github(login)
    rows = @pgsql.exec('SELECT identity FROM human WHERE github = $1', [login])
    raise Nb::Urror("There is no user @#{login} Github user registered here yet") if rows.empty?
    take(rows[0]['identity'])
  end

  def github?(login)
    !@pgsql.exec('SELECT identity FROM human WHERE github = $1', [login]).empty?
  end
end
