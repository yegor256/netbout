# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require_relative 'nb'

# Telechats.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::Telechats
  def initialize(pgsql)
    @pgsql = pgsql
  end

  def identity_of(chat)
    @pgsql.exec('SELECT identity FROM human WHERE telechat = $1', [chat])[0]['identity']
  end

  def exists?(chat)
    !@pgsql.exec('SELECT identity FROM human WHERE telechat = $1', [chat]).empty?
  end
end
