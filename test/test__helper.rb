# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
# SPDX-License-Identifier: MIT

ENV['RACK_ENV'] = 'test'

require 'simplecov'
SimpleCov.start

require 'simplecov-cobertura'
SimpleCov.formatter = SimpleCov::Formatter::CoberturaFormatter

require 'yaml'
require 'minitest/autorun'
require 'pgtk/pool'
require 'loog'
require 'securerandom'

module Minitest
  class Test
    def test_pgsql
      # rubocop:disable Style/ClassVars
      @@test_pgsql ||= Pgtk::Pool.new(
        Pgtk::Wire::Yaml.new(File.join(__dir__, '../target/pgsql-config.yml')),
        log: Loog::VERBOSE
      ).start
      # rubocop:enable Style/ClassVars
    end

    def test_name
      "jeff#{SecureRandom.hex(8)}"
    end
  end
end
