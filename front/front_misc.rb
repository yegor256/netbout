# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

get '/robots.txt' do
  content_type 'text/plain'
  "User-agent: *\nDisallow: /"
end

get '/version' do
  content_type 'text/plain'
  require_relative '../objects/version'
  Nb::VERSION
end

not_found do
  status 404
  content_type 'text/html', charset: 'utf-8'
  haml :not_found, locals: merged(
    title: request.url
  )
end

error do
  status 503
  e = env['sinatra.error']
  if e.is_a?(Nb::Urror)
    flash(@locals[:human] ? iri.cut('/inbox') : iri.cut('/'), e.message, color: 'darkred')
  else
    Raven.capture_exception(e)
    haml(
      :error,
      locals: merged(
        title: 'error',
        error: "#{e.message}\n\t#{e.backtrace.join("\n\t")}"
      )
    )
  end
end

def merged(hash)
  out = @locals.merge(hash)
  out[:local_assigns] = out
  if cookies[:flash_msg]
    out[:flash_msg] = cookies[:flash_msg]
    cookies.delete(:flash_msg)
  end
  out[:flash_color] = cookies[:flash_color] || 'darkgreen'
  cookies.delete(:flash_color)
  out
end

def flash(uri, msg = '', color: 'darkgreen')
  cookies[:flash_msg] = msg
  cookies[:flash_color] = color
  response.headers['X-Netbout-Requested'] = request.url
  response.headers['X-Netbout-Flash'] = msg
  redirect(uri)
end
