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

require 'minitest/autorun'
require_relative 'test__helper'
require_relative '../objects/nb'
require_relative '../objects/humans'
require_relative '../objects/bouts'
require_relative '../objects/urror'

# Test of Bout.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2024 Yegor Bugayenko
# License:: MIT
class Nb::BoutTest < Minitest::Test
  def test_access_denied_on_post
    owner = Nb::Humans.new(test_pgsql).take(test_name).create
    guest = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = owner.bouts
    id = bouts.start('hi').id
    owner.bouts.take(id).post('Hello!')
    bout = guest.bouts.take(id)
    assert(!bout.mine?)
    assert_raises Nb::Urror do
      bout.post('I can see you :(')
    end
  end

  def test_checks_existence
    owner = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = owner.bouts
    id = bouts.start('hi').id
    assert(owner.bouts.take(id).exists?)
  end

  def test_prohibits_existence_checking
    owner = Nb::Humans.new(test_pgsql).take(test_name).create
    guest = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = owner.bouts
    id = bouts.start('hi').id
    bout = guest.bouts.take(id)
    assert(!bout.mine?)
    assert(bout.exists?)
  end

  def test_turns_into_json
    owner = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = owner.bouts
    bout = bouts.start('hi, друг!')
    json = bout.to_h
    assert(json[:id].positive?)
    assert(json[:title].include?('друг'))
    assert(json[:created] < Time.now)
    assert(json[:tags].empty?)
    assert(json[:guests].empty?)
  end

  def test_checks_permission
    owner = Nb::Humans.new(test_pgsql).take(test_name).create
    friend = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = owner.bouts
    bout = bouts.start('hi')
    bout.post(test_name)
    assert(!friend.bouts.take(bout.id).mine?)
    bout.guests.invite(friend.identity)
    assert(friend.bouts.take(bout.id).mine?)
  end
end
