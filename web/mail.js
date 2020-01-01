function select(button)
{
	$.ajax
	({
		type:'GET',
		dataType:'text',
		url:'./mailadmin',
		data:'operation=select&'+button.parents('form').serialize(),
		success:function(data)
		{
			data=eval('('+data+')');
			var columns;
			switch($('#mailbox').val())
			{
				case '':
					columns={
						'Account':{'key':'username','type':'field'},
						'Mailbox':{'key':'status','type':'field'},
						'Subject':{'key':'subject','type':'field'},
						'Time':{'key':'mtime','type':'field'},
						'Operation':{'key':'','type':''}
					}
					break;
				case 'R':
					columns={
						'Account':{'key':'username','type':'field'},
						'Subject':{'key':'subject','type':'field'},
						'Sender':{'key':'sender','type':'field'},
						'Time':{'key':'mtime','type':'field'},
						'Operation':{'key':'','type':''}
					}
					break;
				case 'S':
				case 'D':
					columns={
						'Account':{'key':'username','type':'field'},
						'Subject':{'key':'subject','type':'field'},
						'Receivers':{'key':'receivers','type':'field'},
						'Time':{'key':'mtime','type':'field'},
						'Operation':{'key':'','type':''}
					}
					break;
			}
			if($('#account').val()!='')
				delete columns.Account;
			var headline='';
			for(column in columns)
				headline+="<span class='td'>"+column+"</span>";
			$('div#headline').html(headline);
			$('div#showTable').html('');
			data.forEach(function(result)
			{
				var resultForm=
					"<form class='tr'>\
					<input type='hidden' name='id' value='"+result['id']+"'>\
					<input type='hidden' name='status' value='"+result['status']+"'>";
				for(column in columns)
				{
					var columnInfo=columns[column];
					var key=columnInfo.key;
					if(key=='')
						continue;
					var value=result[key]!=null?result[key]:'';
					if(key=='status')
					{
						switch(value)
						{
							case 'R':
								value='Inbox';
								break;
							case 'S':
								value='Sent';
								break;
							case 'D':
								value='Drafts';
								break;
						}
					}
					resultForm+="<input class='td' type='"+(columnInfo.type!='password'?'text':'password')+"' name='"+key+"' value='"+value+"' list='"+key+"' oninput='updateable($(this))' readonly>";
				}
				resultForm+=
					"<div class='td tr'>\
					<div class='td tb'>\
					<button type='button' onclick='view($(this));'>View</button>\
					</div>\
					<div class='td tb'>\
					<button type='button' onclick='del($(this));'>Delete</button>\
					</div>\
					</div>\
					</form>";
				$('div#showTable').append(resultForm);
			});
			$(window).resize();
		},
		error:function(){errorTip('Failed to get mails!');}
	});
}

function view(button)
{
	$.ajax
	({
		type:'GET',
		dataType:'text',
		url:'./mailadmin',
		data:'operation=view&'+button.parents('form').serialize(),
		success:function(data)
		{
			data=eval('('+data+')');
			$('#id').val(button.parents('form').find('input[name=id]').val());
			$('#sender').val(data['sender']);
			$('#sendertxt').val(data['sender']);
			$('#receivers').val(data['receivers']);
			$('#subject').val(data['subject']);
			$('#date').val(data['date']);
			$('#content').val(data['content']);
			var attachments="<a href='./download?id="+$('#id').val()+"' target='_blank'>Source</a>";
			var i=0;
			data['attachments'].forEach(function(result){
				attachments+="&nbsp;&nbsp;&nbsp;&nbsp;<a href='./download?id="+$('#id').val()+"&aid="+(i++)+"' target='_blank'>"+result+"</a>";
			});
			$('#attachments').html(attachments);
			$('#floatingwindow').find('.R,.S,.D').css('display','none');
			switch(button.parents('form').find('input[name=status]').val())
			{
				case 'R':
					$('#floatingwindow .R').css('display','inherit');
					$('#floatingwindow tr.R').css('display','table-row');
					$('#floatingwindow').find('input,select,textarea').attr('readonly',true);
					break;
				case 'S':
					$('#floatingwindow .S').css('display','inherit');
					$('#floatingwindow tr.S').css('display','table-row');
					$('#floatingwindow').find('input,select,textarea').attr('readonly',true);
					break;
				case 'D':
					$('#floatingwindow .D').css('display','inherit');
					$('#floatingwindow tr.D').css('display','table-row');
					$('#floatingwindow').find('input,select,textarea').attr('readonly',false);
					break;
			}
			$('#floatingwindow').css('display','inherit');
		},
		error:function(){errorTip('Failed to load the mail!');}
	});
}

function viewnew()
{
	$('#id').val('');
	$('#sender').val($('#account').val());
	$('#sendertxt').val('');
	$('#receivers').val('');
	$('#subject').val('');
	$('#date').val('');
	$('#content').val('');
	$('#attachments').html("<input type='file' multiple>");
	$('#floatingwindow').find('.R,.S').css('display','none');
	$('#floatingwindow .D').css('display','inherit');
	$('#floatingwindow tr.D').css('display','table-row');
	$('#floatingwindow').find('input,select,textarea').attr('readonly',false);
	$('#floatingwindow').css('display','inherit');
}

function retr(button)
{
	$.ajax
	({
		type:'POST',
		dataType:'text',
		url:'./mailadmin',
		data:'operation=retrieve&'+button.parents('form').serialize(),
		success:function(data)
		{
			data=eval('('+data+')');
			var i=1;
			var accounts=$('#account option');
			data.forEach(function(result)
			{
				if(!result)
					errorTip('Failed to retrieve mails from account "'+accounts[i].innerHTML+'" !');
				i++;
			});
			select(button);
		},
		error:function(){errorTip('Failed to retrieve mails!');}
	});
	$(window).resize();
}

function update(button)
{
	$('#username').val($('#sender').val());
	$.ajax
	({
		type:'POST',
		dataType:'text',
		url:'./mailadmin',
		data:'operation=update&'+button.parents('form').serialize(),
		success:function(data)
		{
			data=eval('('+data+')')[0];
			if(data)
			{
				closewindow();
				select($('#account'));
			}
			else
				errorTip('Failed to save the mail!');
		},
		error:function(){errorTip('Failed to save the mail!');}
	});
}

function send(button)
{
	$('#username').val($('#sender').val());
	$.ajax
	({
		type:'POST',
		dataType:'text',
		url:'./mailadmin',
		data:'operation=send&'+button.parents('form').serialize(),
		success:function(data)
		{
			data=eval('('+data+')')[0];
			if(data)
			{
				closewindow();
				select($('#account'));
			}
			else
				errorTip('Failed to send the mail!');
		},
		error:function(){errorTip('Failed to send the mail!');}
	});
}

function del(button)
{
	$.ajax
	({
		type:'POST',
		dataType:'text',
		url:'./mailadmin',
		data:'operation=delete&'+button.parents('form').serialize(),
		success:function(data)
		{
			data=eval('('+data+')')[0];
			if(data)
				button.parents('form').remove();
			else
				errorTip('Failed to delete the mail!');
		},
		error:function(){errorTip('Failed to delete the mail!');}
	});
}

function errorTip(message)
{
	var tip='Error!\n'+message;
	if(message=='Offline!')
		tip+='\nPlease check your network connection and try again!';
	else
		tip+='\nPlease check your settings and try again!';
	alert(tip);
}

function closewindow()
{
	$('#floatingwindow').css('display','none');
}

$(window).resize(function()
{
	$('div#blankTable').css('height',$('div#headTable').css('height'));
});
$.ajax
({
	type:'GET',
	dataType:'text',
	url:'./accountadmin',
	success:function(data)
	{
		data=eval('('+data+')');
		data.forEach(function(result)
		{
			var option="<option value='"+result+"'>"+result+"</option>";
			$('#account').append(option);
			$('#sender').append(option);
		});
	},
	error:function(){errorTip('Failed to get account list!');}
});
select($('#account'));
