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
      identity = user[:login]
      identity = user[:id] if identity.nil?
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
