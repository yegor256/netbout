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
require_relative 'urror'

# Bouts of a human.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2024 Yegor Bugayenko
# License:: MIT
class Nb::Bouts
  # When bout is not found by ID
  class BoutNotFound < Nb::Urror; end

  # When title is not corrent
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
    raise BoutNotFound, "The bout ##{id} doesn't exist" unless exists?(id)
    require_relative 'bout'
    Nb::Bout.new(@pgsql, @human, id)
  end

  def exists?(id)
    !@pgsql.exec('SELECT id FROM bout WHERE id = $1', [id]).empty?
  end
end
