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

# Test of Humans.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2024 Yegor Bugayenko
# License:: MIT
class Nb::HumansTest < Minitest::Test
  def test_adds_and_reads
    identity = test_name
    humans = Nb::Humans.new(test_pgsql)
    human = humans.take(identity)
    assert(human.identity == identity)
    assert(!human.exists?)
    human.create
    assert(human.exists?)
  end

  def test_adds_and_finds_by_github
    identity = test_name
    login = test_name
    humans = Nb::Humans.new(test_pgsql)
    assert(!humans.github?(login))
    human = humans.take(identity)
    human.create
    human.github = login
    assert(humans.github?(login))
    assert(humans.take_by_github(login).exists?)
  end

  def test_starts_bout
    human = Nb::Humans.new(test_pgsql).take(test_name)
    human.create
    bout = human.bouts.start('hello!')
    assert(bout.id.positive?)
  end
end
