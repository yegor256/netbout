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
require_relative 'bout'
require_relative 'humans'
require_relative 'flags'

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
    !@pgsql.exec('SELECT author FROM message WHERE id = $1', [@id]).empty?
  end

  def text
    raise Nb::Urror, "#{@human} can't read message ##{@id}" unless mine?
    @pgsql.exec('SELECT text FROM message WHERE id = $1', [@id])[0]['text']
  end

  def created
    raise Nb::Urror, "#{@human} can't read message ##{@id}" unless mine?
    time = @pgsql.exec('SELECT created FROM message WHERE id = $1', [@id])[0]['created']
    Time.parse(time)
  end

  def author
    raise Nb::Urror, "#{@human} can't read message ##{@id}" unless mine?
    author = @pgsql.exec('SELECT author FROM message WHERE id = $1', [@id])[0]['author']
    Nb::Humans.new(@pgsql).take(author)
  end

  def bout
    raise Nb::Urror, "#{@human} can't read message ##{@id}" unless mine?
    bout = @pgsql.exec('SELECT bout FROM message WHERE id = $1', [@id])[0]['bout'].to_i
    Nb::Bout.new(@pgsql, @human, bout)
  end

  def flags
    raise Nb::Urror, "#{@human} can't read message ##{@id}" unless mine?
    Nb::Flags.new(@pgsql, @human, self)
  end

  def to_h
    raise Nb::Urror, "#{@human} can't serialize message ##{@id}" unless mine?
    {
      id: @id,
      bout: bout.id,
      text: text,
      created: created,
      author: author.identity,
      flags: flags.to_a
    }
  end
end
