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

# Bout of a user (reader).
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2024 Yegor Bugayenko
# License:: MIT
class Nb::Bout
  attr_reader :id

  def initialize(pgsql, human, id)
    @pgsql = pgsql
    raise 'Human is NULL' if human.nil?
    @human = human
    raise 'Id is NULL' if id.nil?
    @id = id
  end

  def exists?
    !@pgsql.exec('SELECT * FROM bout WHERE id = $1', [@id]).empty?
  end

  def mine?
    !@pgsql.exec(
      [
        'SELECT id FROM bout',
        'LEFT JOIN guest ON bout.id=guest.bout',
        'WHERE bout.id=$2 AND bout.owner=$1 OR guest.human=$1',
        'LIMIT 1'
      ],
      [@human.identity, @id]
    ).empty?
  end

  def owner
    raise Nb::Urror, "#{@human} can't touch bout ##{@id}" unless mine?
    @pgsql.exec('SELECT owner FROM bout WHERE id = $1', [@id])[0]['owner']
  end

  def created
    raise Nb::Urror, "#{@human} can't touch bout ##{@id}" unless mine?
    time = @pgsql.exec('SELECT created FROM bout WHERE id = $1', [@id])[0]['created']
    Time.parse(time)
  end

  def title
    raise Nb::Urror, "#{@human} can't touch bout ##{@id}" unless mine?
    @pgsql.exec('SELECT title FROM bout WHERE id = $1', [@id])[0]['title']
  end

  def post(text)
    raise Nb::Urror, "#{@human} can't post to bout ##{@id}" unless mine?
    rows = @pgsql.exec(
      'INSERT INTO message (author, bout, text) VALUES ($1, $2, $3) RETURNING id',
      [@human.identity, @id, text]
    )
    id = rows[0]['id'].to_i
    require_relative 'message'
    Nb::Message.new(@pgsql, @human, id)
  end

  def tags
    raise Nb::Urror, "#{@human} can't read tags of bout ##{@id}" unless mine?
    require_relative 'tags'
    Nb::Tags.new(@pgsql, @human, self)
  end

  def guests
    raise Nb::Urror, "#{@human} can't read guests of bout ##{@id}" unless mine?
    require_relative 'guests'
    Nb::Guests.new(@pgsql, @human, self)
  end

  def to_h
    raise Nb::Urror, "#{@human} can't serialize bout ##{@id}" unless mine?
    {
      id: @id,
      owner: owner,
      created: created,
      title: title,
      tags: tags.to_a,
      guests: guests.to_a
    }
  end
end
