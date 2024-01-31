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
  array = []
  search.each do |msg|
    array << msg.to_h
  end
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
