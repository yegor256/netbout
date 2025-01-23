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

# Test of Flags.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2025 Yegor Bugayenko
# License:: MIT
class Nb::FlagsTest < Minitest::Test
  def test_attaches_flags_to_message
    human = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = human.bouts
    bout = bouts.start('hi')
    msg = bout.post('How are you?')
    flag = msg.flags.attach('hey')
    assert(flag.exists?)
    msg.flags.each do |f|
      assert(f.exists?)
    end
  end

  def test_attaches_and_detaches
    human = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = human.bouts
    bout = bouts.start('ooo')
    msg = bout.post('hey you')
    key = 'the-flag'
    flag = msg.flags.attach(key)
    assert(flag.exists?)
    flag.detach
    assert(!flag.exists?)
  end
end
