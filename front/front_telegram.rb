# frozen_string_literal: true

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

require 'telebot'
require_relative '../objects/urror'

get '/telegram' do
  id = params[:id].to_i
  telechats.add(id, current_human)
  telepost("We identified you as [@#{current_human}](https://netbout.com/u/#{current_human}), thanks!")
  flash('/', "Your account linked with Telegram chat ##{id}, thanks!")
end

def telechats
  require_relative '../objects/telechats'
  @telechats ||= Nb::Telechats.new(settings.pgsql)
end

def telebot
  return nil unless settings.config['telegram']
  @telebot ||= Telebot::Client.new(settings.config['telegram']['token'])
end

def telepost(msg, chat = telechats.chat_of(current_human), reply_markup: nil)
  return unless settings.config['telegram']
  telechats.posted(msg, chat)
  telebot.send_message(
    chat_id: chat,
    parse_mode: 'Markdown',
    disable_web_page_preview: true,
    text: msg.length > 4000 ? "#{msg[0..4000]}..." : msg,
    reply_markup: reply_markup
  )
end

def reply(msg, login)
  case msg
  when %r{^/done$}
    left = tasks(login: login).fetch(limit: 100)
    if left.empty?
      ['There are no tasks in your agenda, nothing to complete.']
    elsif left.count > 16
      [
        "There are #{left.count} tasks in your agenda.",
        'Just pick one and say `/done <id>` and I will understand you.',
        "I can't show you a menu, because you've got so many tasks (#{left.count}),",
        'which is an obvious sign of your management problems :('
      ]
    else
      {
        keyboard: left.sort_by { |t| t[:id] }.map.with_index do |t, i|
          {
            index: i,
            text: "/done #{t[:id]}"
          }
        end.group_by { |x| (x[:index] / 4).round }.values,
        one_time_keyboard: true,
        resize_keyboard: true
      }
    end
  when %r{^/done [0-9]+$}
    id = msg.split[1].to_i
    tasks(login: login).done(id)
    left = tasks(login: login).fetch
    [
      "Task `T#{id}` was marked as completed, thanks!",
      left.empty? ?
        'Your agenda is empty, good job!' :
        "There are still #{left.count} tasks in your agenda. Say /tasks to see them all."
    ]
  when %r{^/tasks$}
    list = tasks(login: login).fetch(limit: 100)
    if list.empty?
      ['There are no tasks in your agenda, good job!']
    else
      task_list(list)
    end
  else
    [
      "I didn't understand you, but I'm still with you, [#{login}](https://github.com/#{login})!",
      'In this chat I inform you about the most important tasks you have in your agenda',
      'in [0rsk.com](https://www.0rsk.com).'
    ]
  end
end

if settings.config['telegram']
  Thread.new do
    Telebot::Bot.new(settings.config['telegram']['token']).run do |_, message|
      chat = message.chat.id
      if telechats.exists?(chat)
        process_request(chat, message)
      else
        telepost("[Click here](https://www.0rsk.com/telegram?id=#{chat}) to identify yourself.", chat)
      end
    end
  rescue Net::ReadTimeout => e
    settings.log.error(e.message)
  end
end

def process_request(chat, message)
  identity = telechats.identity_of(chat)
  telepost("Hello, #{identity}! (#{message})", chat)
end
