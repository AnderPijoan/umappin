items = new SubItems
$('button').click () ->
  console.log window.localStorage
  item = new SubItem
    itemName: $('#nameInput').val()
    itemDesc: $('#descInput').val()
  if $('#idInput').val() != '' then item.set id: $('#idInput').val()
  switch $(@).text()
    when 'GET'
      if item.get('id') then item.fetch() else items.fetch()
    when 'PUT', 'POST' then item.save()
    when 'DELETE' then item.destroy()
