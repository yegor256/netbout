# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require 'minitest/autorun'
require_relative 'test__helper'
require_relative '../objects/nb'
require_relative '../objects/humans'

# Test of Message.
# Author:: Yegor Bugayenko (yegor256@gmail.com)
# Copyright:: Copyright (c) 2009-2026 Yegor Bugayenko
# License:: MIT
class Nb::MessageTest < Minitest::Test
  def test_checks_owner_and_guest_permission
    owner = Nb::Humans.new(test_pgsql).take(test_name).create
    guest = Nb::Humans.new(test_pgsql).take(test_name).create
    bout = owner.bouts.start('Shared')
    message = bout.post('Hello')
    assert_predicate(message, :mine?)
    assert_equal('Hello', message.text)
    invited = guest.messages.take(message.id)
    refute_predicate(invited, :mine?)
    assert_raises(Nb::Urror) { invited.text }
    bout.guests.invite(guest.identity)
    assert_predicate(invited, :mine?)
    assert_equal('Hello', invited.text)
  end

  def test_denies_access_with_an_unrelated_owned_message
    owner = Nb::Humans.new(test_pgsql).take(test_name).create
    reader = Nb::Humans.new(test_pgsql).take(test_name).create
    reader.bouts.start('Mine').post('My message')
    private_message = owner.bouts.start('Private').post('Secret')
    message = reader.messages.take(private_message.id)
    refute_predicate(message, :mine?)
    assert_raises(Nb::Urror) { message.text }
  end

  def test_denies_access_with_an_unrelated_invitation
    owner = Nb::Humans.new(test_pgsql).take(test_name).create
    guest = Nb::Humans.new(test_pgsql).take(test_name).create
    shared = owner.bouts.start('Shared')
    shared.guests.invite(guest.identity)
    shared.post('Shared message')
    private_message = owner.bouts.start('Private').post('Secret')
    message = guest.messages.take(private_message.id)
    refute_predicate(message, :mine?)
    assert_raises(Nb::Urror) { message.text }
  end

  def test_turns_into_json
    owner = Nb::Humans.new(test_pgsql).take(test_name).create
    bouts = owner.bouts
    bout = bouts.start('hi, друг!')
    msg = bout.post('how are you, друг?')
    json = msg.to_h
    assert_predicate(json[:id], :positive?)
    assert_includes(json[:text], 'друг')
    assert_operator(json[:created], :<, Time.now)
    assert_empty(json[:flags])
  end
end
