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

require 'minitest/autorun'
require_relative 'test__helper'
require_relative '../objects/nb'
require_relative '../objects/humans'
require_relative '../objects/bouts'

# Test of Guests.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2025 Yegor Bugayenko
# License:: MIT
class Nb::GuestsTest < Minitest::Test
  def test_invites_to_bout
    human = Nb::Humans.new(test_pgsql).take(test_name).create
    friend = Nb::Humans.new(test_pgsql).take(test_name).create
    id = human.bouts.start('hi').id
    human.bouts.take(id).guests.invite(friend.identity)
    assert(human.bouts.take(id).mine?)
    bout = friend.bouts.take(id)
    assert(bout.mine?)
    found = []
    bout.guests.each do |t|
      found << t
    end
    assert_equal(1, found.size)
    assert_equal(friend.identity, found.first)
  end
end
