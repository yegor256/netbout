# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require 'digest'
require 'securerandom'
require_relative 'nb'
require_relative 'urror'

# Auth tokens of a human.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::Tokens
  def initialize(pgsql, human)
    @pgsql = pgsql
    raise 'Human is NULL' if human.nil?
    @human = human
  end

  def get
    rows = @pgsql.exec('SELECT sha FROM token WHERE human=$1', [@human.identity])
    return rows[0]['sha'] unless rows.empty?
    sha = Digest::SHA1.hexdigest(SecureRandom.hex(64))
    @pgsql.exec('INSERT INTO token (human, sha) VALUES ($1, $2)', [@human.identity, sha])
    sha
  end
end
