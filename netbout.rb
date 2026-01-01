# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

$stdout.sync = true

require 'glogin'
require 'glogin/codec'
require 'haml'
require 'iri'
require 'loog'
require 'json'
require 'cgi'
require 'pgtk'
require 'pgtk/pool'
require 'raven'
require 'relative_time'
require 'sinatra'
require 'sinatra/cookies'
require 'telebot'
require 'time'
require 'yaml'
require_relative 'objects/urror'
require_relative 'objects/query'

unless ENV['RACK_ENV'] == 'test'
  require 'rack/ssl'
  use Rack::SSL
end

configure do
  config = {
    'github' => {
      'client_id' => '?',
      'client_secret' => '',
      'encryption_secret' => ''
    },
    'sentry' => ''
  }
  unless ENV['RACK_ENV'] == 'test'
    f = File.join(File.dirname(__FILE__), 'config.yml')
    unless File.exist?(f)
      raise [
        "The config file #{f} is absent, can't start the app. ",
        "If you are running in a staging/testing mode, set RACK_ENV envirornemt variable to 'test'"
      ].join
    end
    config = YAML.safe_load(File.open(f))
  end
  unless ENV['RACK_ENV'] == 'test'
    Raven.configure do |c|
      c.dsn = config['sentry']
      require_relative 'objects/version'
      c.release = Nb::VERSION
    end
  end
  set :bind, '0.0.0.0'
  set :show_exceptions, false
  set :raise_errors, false
  set :dump_errors, true
  set :config, config
  set :logging, true
  set :log, Loog::REGULAR
  set :server, :webrick
  set :server_settings, timeout: 25
  set :glogin, GLogin::Auth.new(
    config['github']['client_id'],
    config['github']['client_secret'],
    'https://www.netbout.com/github-callback'
  )
  if File.exist?('target/pgsql-config.yml')
    set :pgsql, Pgtk::Pool.new(
      Pgtk::Wire::Yaml.new(File.join(__dir__, 'target/pgsql-config.yml')),
      log: Loog::NULL
    )
  else
    set :pgsql, Pgtk::Pool.new(
      Pgtk::Wire::Env.new('DATABASE_URL'),
      log: settings.log
    )
  end
  settings.pgsql.start(4)
end

get '/' do
  flash(iri.cut('/inbox')) if @locals[:human]
  haml :index, locals: merged(
    title: '/'
  )
end

get '/inbox' do
  offset = [(params[:offset] || '0').to_i, 0].max
  limit = (params[:limit] || '10').to_i
  haml :inbox, locals: merged(
    title: '/inbox',
    q: params[:q] || '',
    limit: limit,
    offset: offset
  )
end

get '/start' do
  haml :start, locals: merged(
    title: '/start'
  )
end

post '/start' do
  title = params[:title]
  flash(iri.cut('/start'), "The title can't be empty") if title.nil?
  bout = current_human.bouts.start(title)
  response.headers['X-Netbout-Bout'] = bout.id.to_s
  flash(iri.cut('/b').append(bout.id), "The bout ##{bout.id} started")
end

get '/b/{id}' do
  id = params[:id].to_i
  response.headers['X-Netbout-Bout'] = id.to_s
  redirect(iri.cut('/inbox').over(q: "(bout=#{id})"))
end

post '/b/{id}/post' do
  bout = current_human.bouts.take(params[:id].to_i)
  text = params[:text]
  msg = bout.post(text)
  response.headers['X-Netbout-Message'] = msg.id.to_s
  flash(iri.cut('/b').append(bout.id), "Message ##{msg.id} posted to the bout ##{bout.id}")
end

post '/b/{id}/tag' do
  bout = current_human.bouts.take(params[:id].to_i)
  name = params[:name]
  value = params[:value]
  bout.tags.put(name, value)
  flash(iri.cut('/b').append(bout.id), "Tag '##{name}' put to the bout ##{bout.id}")
end

post '/b/{id}/invite' do
  bout = current_human.bouts.take(params[:id].to_i)
  identity = params[:human]
  bout.guests.invite(identity)
  flash(iri.cut('/b').append(bout.id), "User @#{identity}' invited to the bout ##{bout.id}")
end

post '/m/{id}/attach' do
  msg = current_human.messages.take(params[:id].to_i)
  name = params[:name]
  msg.flags.attach(name)
  bout = msg.bout
  flash(iri.cut('/b').append(bout.id), "Flag '##{name}' attached to the bout ##{bout.id}")
end

get '/m/{id}/detach' do
  msg = current_human.messages.take(params[:id].to_i)
  name = params[:name]
  flag = msg.flags.take(name)
  flag.detach
  bout = msg.bout
  flash(iri.cut('/b').append(bout.id), "Flag '##{name}' detached from the bout ##{bout.id}")
end

get '/terms' do
  haml :terms, locals: merged(
    title: '/terms'
  )
end

get '/token' do
  haml :token, locals: merged(
    title: '/token'
  )
end

get '/sql' do
  raise Urror::Nb, 'You are not allowed to see this' unless current_human.admin?
  query = params[:query] || 'SELECT * FROM human LIMIT 5'
  start = Time.now
  result = settings.pgsql.exec(query)
  haml :sql, layout: :layout, locals: merged(
    title: '/sql',
    query: query,
    result: result,
    lag: Time.now - start
  )
end

def current_human
  flash(iri.cut('/'), 'You have to login first') unless @locals[:human]
  @locals[:human]
end

def humans
  require_relative 'objects/humans'
  @humans ||= Nb::Humans.new(settings.pgsql)
end

def iri
  Iri.new(request.url)
end

require_relative 'front/front_telegram'
require_relative 'front/front_misc'
require_relative 'front/front_login'
require_relative 'front/front_api'
