# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require_relative '../objects/version'
require_relative '../objects/tokens'

before '/*' do
  @locals = {
    http_start: Time.now,
    ver: Nb::VERSION,
    github_login_link: settings.glogin.login_uri,
    request_ip: request.ip
  }
  token = request.env['HTTP_X_NETBOUT_TOKEN']
  identity = nil
  identity = humans.find_by_token(token).identity unless token.nil?
  cookies[:identity] = params[:identity] if params[:identity]
  if cookies[:identity]
    begin
      user = GLogin::Cookie::Closed.new(
        cookies[:identity],
        settings.config['github']['encryption_secret']
      ).to_user
      identity = user['login']
      identity = user['id'] if identity.nil?
    rescue GLogin::Codec::DecodingError
      cookies.delete(:identity)
    end
  end
  unless identity.nil?
    human = humans.take(identity)
    @locals[:human] = human
    unless human.exists?
      human.create
      human.github = identity
    end
  end
end

get '/github-callback' do
  code = params[:code]
  error(400) if code.nil?
  json = settings.glogin.user(code)
  cookies[:identity] = GLogin::Cookie::Open.new(
    json, settings.config['github']['encryption_secret']
  ).to_s
  flash(iri.cut('/'), "@#{json['login']} has been logged in")
end

get '/logout' do
  cookies.delete(:identity)
  flash(iri.cut('/'), 'You have been logged out')
end
