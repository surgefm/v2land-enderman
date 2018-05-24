
import * as React from "react";
import * as ReactDOM from "react-dom";

import { AppState, IAppMode } from "./models";
import {observer} from 'mobx-react';
import DevTools from 'mobx-react-devtools';

import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import IconButton from '@material-ui/core/IconButton';

import { sleep } from './utils';

import "./style.scss";

@observer
class AppView extends React.Component<{appState: AppState}, {}> {

  render() {
    return (
      <div className="app">
        <AppBar position="static" color="default">
          <Toolbar>
            <Typography variant="title" color="inherit">
              Enderman
            </Typography>
          </Toolbar>
        </AppBar>
      </div>
    );
  }

};

const appState = new AppState();
ReactDOM.render(<AppView appState={appState} />, document.getElementById("app"));
