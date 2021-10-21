
function [Password, UserName] = passwordEntryDialog( )
%
% [Password, UserName] = passwordEntryDialog( )
%

  UserName = [];
  Password = [];

  prompt = {"Warning password is not HIDDEN.\n\nUsername:", "Password:"};
  defaults = {"",""};
  rowscols = [1,10;1,10];
  dims = inputdlg (prompt, "Password", rowscols, defaults);
  
  if !isempty( dims )
  
    UserName = dims{ 1 };
    Password = dims{ 2 };

   end
end
