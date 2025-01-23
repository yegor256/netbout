# frozen_string_literal: true

# (The MIT License)
#
# Copyright (c) 2009-2025 Yegor Bugayenko
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

# Tag of a bout.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2025 Yegor Bugayenko
# License:: MIT
class Nb::Tag
  attr_reader :name

  def initialize(pgsql, bout, name)
    @pgsql = pgsql
    raise 'Bout is NULL' if bout.nil?
    @bout = bout
    raise 'Name is NULL' if name.nil?
    @name = name
  end

  def exists?
    !@pgsql.exec('SELECT * FROM tag WHERE bout=$1 AND name=$2', [@bout.id, @name]).empty?
  end

  def created
    @pgsql.exec('SELECT created FROM tag WHERE bout=$1 AND name=$2', [@bout.id, @name])[0]['created']
  end

  def value
    row = @pgsql.exec('SELECT value FROM tag WHERE bout=$1 AND name=$2', [@bout.id, @name])[0]
    raise Nb::Urror, "Tag '#{@name}' not found in the bout ##{@bout.id}" if row.nil?
    row['value']
  end

  def to_h
    {
      name: @name,
      value: value,
      created: created
    }
  end
end
