# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require_relative 'nb'
require_relative 'urror'

# Messages of a human.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::Messages
  # When message is not found by ID
  class MessageNotFound < Nb::Urror; end

  def initialize(pgsql, human)
    @pgsql = pgsql
    raise 'Human is NULL' if human.nil?
    @human = human
  end

  def take(id)
    raise MessageNotFound("The message ##{id} doesn't exist") unless exists?(id)
    require_relative 'message'
    Nb::Message.new(@pgsql, @human, id)
  end

  def exists?(id)
    !@pgsql.exec('SELECT id FROM message WHERE id = $1', [id]).empty?
  end
end
