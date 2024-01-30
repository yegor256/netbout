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

# Message of a user (reader).
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2024 Yegor Bugayenko
# License:: MIT
class Nb::Message
  attr_reader :id

  def initialize(pgsql, human, id)
    @pgsql = pgsql
    raise 'Human is NULL' if human.nil?
    @human = human
    raise 'Id is NULL' if id.nil?
    @id = id
  end

  def mine?
    !@pgsql.exec(
      [
        'SELECT message.id FROM message',
        'LEFT JOIN bout ON message.bout=bout.id',
        'LEFT JOIN guest ON message.bout=guest.bout',
        'WHERE bout.owner=$1 OR guest.human=$1 AND message.id=$2',
        'LIMIT 1'
      ],
      [@human.identity, @id]
    ).empty?
  end

  def exists?
    !@pgsql.exec('SELECT * FROM message WHERE id = $1', [@id]).empty?
  end

  def text
    @pgsql.exec('SELECT text FROM message WHERE id = $1', [@id])[0]['text']
  end

  def created
    time = @pgsql.exec('SELECT created FROM message WHERE id = $1', [@id])[0]['created']
    Time.parse(time)
  end

  def author
    author = @pgsql.exec('SELECT author FROM message WHERE id = $1', [@id])[0]['author']
    require_relative 'humans'
    Nb::Humans.new(@pgsql).take(author)
  end

  def bout
    bout = @pgsql.exec('SELECT bout FROM message WHERE id = $1', [@id])[0]['bout'].to_i
    require_relative 'bout'
    Nb::Bout.new(@pgsql, @human, bout)
  end

  def flags
    require_relative 'flags'
    Nb::Flags.new(@pgsql, @human, self)
  end

  def to_h
    {
      id: @id,
      text: text,
      created: created,
      author: author.identity,
      flags: flags.to_a
    }
  end
end
