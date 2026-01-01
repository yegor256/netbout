# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require_relative '../objects/query'

get '/self' do
  content_type 'application/json'
  {
    identity: current_human.identity
  }.to_json
end

get '/search' do
  query = params[:query] || ''
  offset = [(params[:offset] || '0').to_i, 0].max
  limit = (params[:limit] || '10').to_i
  search = current_human.search(Nb::Query.new(query), offset, limit)
  content_type 'application/json'
  array = search.to_a.map(&:to_h)
  array.to_json
end

get '/bout/{id}' do
  id = params[:id].to_i
  bout = current_human.bouts.take(id)
  content_type 'application/json'
  bout.to_h.to_json
end

get '/tags/{id}' do
  id = params[:id].to_i
  bout = current_human.bouts.take(id)
  content_type 'application/json'
  bout.tags.to_a.to_json
end

get '/message/{id}' do
  id = params[:id].to_i
  msg = current_human.messages.take(id)
  content_type 'application/json'
  msg.to_h.to_json
end

get '/flags/{id}' do
  id = params[:id].to_i
  msg = current_human.messages.take(id)
  content_type 'application/json'
  msg.flags.to_a.to_json
end
