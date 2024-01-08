# frozen_string_literal: true

# (The MIT License)
#
# Copyright (c) 2009-2024 Yegor Bugayenko
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the 'Software'), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

require_relative 'nb'

# Human.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2024 Yegor Bugayenko
# License:: MIT
class Nb::Human
  attr_reader :identity

  def initialize(pgsql, identity)
    @pgsql = pgsql
    raise 'Identity is NULL' if identity.nil?
    @identity = identity
  end

  def create
    @pgsql.exec('INSERT INTO human (identity) VALUES ($1)', [@identity])
    self
  end

  def github=(login)
    @pgsql.exec('UPDATE human SET github = $1 WHERE identity = $2', [login, @identity])
  end

  def exists?
    !@pgsql.exec('SELECT * FROM human WHERE identity = $1', [@identity]).empty?
  end

  def telechat?
    !@pgsql.exec('SELECT telechat FROM human WHERE identity = $1', [@identity]).empty?
  end

  def bouts
    require_relative 'bouts'
    Nb::Bouts.new(@pgsql, @identity)
  end

  def messages
    require_relative 'messages'
    Nb::Messages.new(@pgsql, @identity)
  end

  def search(query, offset, limit)
    require_relative 'search'
    Nb::Search.new(@pgsql, @identity, query, offset, limit)
  end
end
